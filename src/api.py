import os
import time
import logging
from urllib.parse import urlparse
import requests

from flask import Flask, request, jsonify
from flask_cors import CORS

from predict import predict_url


app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 16 * 1024

allowed_origins = [
    origin.strip()
    for origin in os.getenv("CORS_ORIGINS", "").split(",")
    if origin.strip()
]
if allowed_origins:
    CORS(app, origins=allowed_origins)

logger = logging.getLogger(__name__)


def check_virustotal(url):
    """
    Submit a URL to VirusTotal and retrieve its analysis result.
    """

    api_key = os.getenv("VT_API_KEY")

    if not api_key:
        return {
            "available": False,
            "error": "VirusTotal API key not configured"
        }

    headers = {
        "x-apikey": api_key
    }

    try:

        response = requests.post(
            "https://www.virustotal.com/api/v3/urls",
            headers=headers,
            data={"url": url},
            timeout=15
        )

        if response.status_code not in (200, 201):
            return {
                "available": False,
                "error": f"VirusTotal returned HTTP {response.status_code}"
            }

        analysis_id = response.json()["data"]["id"]

        analysis_url = (
            f"https://www.virustotal.com/api/v3/analyses/"
            f"{analysis_id}"
        )

        stats = None

        for _ in range(5):

            analysis_response = requests.get(
                analysis_url,
                headers=headers,
                timeout=15
            )

            if analysis_response.status_code != 200:
                return {
                    "available": False,
                    "error": (
                        "Could not retrieve VirusTotal analysis "
                        f"(HTTP {analysis_response.status_code})"
                    )
                }

            analysis_data = analysis_response.json()

            attributes = analysis_data["data"]["attributes"]

            status = attributes.get("status")

            if status == "completed":
                stats = attributes.get("stats", {})
                break

            time.sleep(2)

        if stats is None:
            return {
                "available": True,
                "status": "pending",
                "message": "VirusTotal analysis is still processing"
            }

        return {
            "available": True,
            "status": "completed",
            "malicious": stats.get("malicious", 0),
            "suspicious": stats.get("suspicious", 0),
            "harmless": stats.get("harmless", 0),
            "undetected": stats.get("undetected", 0)
        }

    except requests.RequestException:
        logger.exception("VirusTotal request failed")
        return {
            "available": False,
            "error": "VirusTotal request failed"
        }


def calculate_risk(prediction, phishing_probability, vt_result):
    """
    Combine the Random Forest prediction and VirusTotal
    results into a simple risk classification.
    """

    vt_malicious = 0

    if vt_result.get("available"):
        vt_malicious = vt_result.get("malicious", 0)

    if prediction == "PHISHING":

        if vt_malicious > 0:
            return "HIGH"

        if phishing_probability >= 0.90:
            return "HIGH"

        return "MEDIUM"

    if vt_malicious > 0:
        return "HIGH"

    return "LOW"


@app.route("/health", methods=["GET"])
def health():

    return jsonify({
        "status": "ok",
        "service": "WhatsThatLink API"
    })


@app.route("/predict", methods=["POST"])
def predict():

    data = request.get_json()

    if not data or "url" not in data:

        return jsonify({
            "error": "URL is required"
        }), 400

    url = data["url"]

    if not isinstance(url, str) or not url.strip():

        return jsonify({
            "error": "URL must be a non-empty string"
        }), 400

    url = url.strip()
    parsed_url = urlparse(url)

    if (
        len(url) > 2048
        or parsed_url.scheme not in {"http", "https"}
        or not parsed_url.hostname
    ):
        return jsonify({
            "error": "URL must be a valid HTTP or HTTPS URL"
        }), 400

    try:

        result = predict_url(url)

        vt_result = check_virustotal(url)

        risk = calculate_risk(
            result["prediction"],
            result["phishing_probability"],
            vt_result
        )

        return jsonify({

            "url": url,

            "prediction": result["prediction"],

            "phishing_probability":
                result["phishing_probability"],

            "legitimate_probability":
                result["legitimate_probability"],

            "risk": risk,

            "virustotal": vt_result
        })

    except Exception:
        logger.exception("Prediction request failed")
        return jsonify({
            "error": "Unable to process the request"
        }), 500


if __name__ == "__main__":
    app.run(
        host=os.getenv("HOST", "127.0.0.1"),
        port=int(os.getenv("PORT", "5000")),
        debug=os.getenv("FLASK_DEBUG", "").lower() == "true"
    )

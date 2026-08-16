from flask import Flask, request, jsonify
import joblib
import pandas as pd
import sys
from pathlib import Path
from urllib.parse import urlparse

sys.path.append(str(Path(__file__).resolve().parent.parent / "src"))

from features import extract_features, FEATURE_COLUMNS


app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 16 * 1024


MODEL_PATH = (
    Path(__file__).resolve().parent.parent
    / "model"
    / "phishing_model.joblib"
)

model = joblib.load(MODEL_PATH)


@app.route("/", methods=["GET"])
def home():

    return jsonify({
        "service": "WhatsThatLink?",
        "status": "running"
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
            "error": "Invalid URL"
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

    feature_dict = extract_features(url)

    features = pd.DataFrame(
        [[
            feature_dict[column]
            for column in FEATURE_COLUMNS
        ]],
        columns=FEATURE_COLUMNS
    )


    prediction = model.predict(features)[0]

    probabilities = model.predict_proba(features)[0]

    phishing_probability = probabilities[
        list(model.classes_).index(0)
    ]

    legitimate_probability = probabilities[
        list(model.classes_).index(1)
    ]


    risk_score = phishing_probability * 100


    if risk_score >= 70:

        risk_level = "HIGH"

    elif risk_score >= 40:

        risk_level = "MEDIUM"

    else:

        risk_level = "LOW"


    result = {

        "url": url,

        "prediction": (
            "PHISHING"
            if prediction == 0
            else "LEGITIMATE"
        ),

        "phishing_probability": round(
            phishing_probability * 100,
            2
        ),

        "legitimate_probability": round(
            legitimate_probability * 100,
            2
        ),

        "risk_score": round(
            risk_score,
            2
        ),

        "risk_level": risk_level
    }


    return jsonify(result)


if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=5000,
        debug=False
    )

import joblib
import pandas as pd
from pathlib import Path

from features import extract_features, FEATURE_COLUMNS


MODEL_PATH = Path(__file__).resolve().parent.parent / "model" / "phishing_model.joblib"
model = joblib.load(MODEL_PATH)


def predict_url(url):
    # Extract features
    feature_dict = extract_features(url)

    # Convert to DataFrame
    features = pd.DataFrame(
        [[feature_dict[column] for column in FEATURE_COLUMNS]],
        columns=FEATURE_COLUMNS
    )

    # Make prediction
    prediction = model.predict(features)[0]

    # Get probabilities
    probabilities = model.predict_proba(features)[0]

    # Model classes are:
    # 0 = phishing
    # 1 = legitimate

    phishing_probability = probabilities[
        list(model.classes_).index(0)
    ]

    legitimate_probability = probabilities[
        list(model.classes_).index(1)
    ]

    if prediction == 0:
        result = "PHISHING"
    else:
        result = "LEGITIMATE"

    return {
        "url": url,
        "prediction": result,
        "phishing_probability": phishing_probability,
        "legitimate_probability": legitimate_probability
    }


# Test URL
if __name__ == "__main__":

    test_url = "https://example.com/login?id=12345"

    result = predict_url(test_url)

    print("\n===== WHATS THAT LINK? =====")

    print(f"URL: {result['url']}")
    print(f"Prediction: {result['prediction']}")
    print(
        f"Phishing probability: "
        f"{result['phishing_probability']:.2%}"
    )
    print(
        f"Legitimate probability: "
        f"{result['legitimate_probability']:.2%}"
    )

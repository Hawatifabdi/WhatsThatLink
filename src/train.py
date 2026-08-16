import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    classification_report,
    confusion_matrix
)

# ==========================================
# 1. LOAD CLEANED DATASET
# ==========================================

df = pd.read_csv("data/processed_urls.csv")

print("Dataset shape:", df.shape)


# ==========================================
# 2. SELECT FEATURES AND TARGET
# ==========================================

feature_columns = [
    "URLLength",
    "DomainLength",
    "IsDomainIP",
    "TLDLength",
    "NoOfSubDomain",
    "HasObfuscation",
    "NoOfObfuscatedChar",
    "ObfuscationRatio",
    "NoOfLettersInURL",
    "LetterRatioInURL",
    "NoOfDegitsInURL",
    "DegitRatioInURL",
    "NoOfEqualsInURL",
    "NoOfQMarkInURL",
    "NoOfAmpersandInURL",
    "NoOfOtherSpecialCharsInURL",
    "SpacialCharRatioInURL"
]

X = df[feature_columns]
y = df["label"]


# ==========================================
# 3. TRAIN / TEST SPLIT
# ==========================================

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

print("\nTraining samples:", len(X_train))
print("Testing samples:", len(X_test))


# ==========================================
# 4. CREATE RANDOM FOREST
# ==========================================

model = RandomForestClassifier(
    n_estimators=200,
    random_state=42,
    n_jobs=-1,
    class_weight="balanced"
)


# ==========================================
# 5. TRAIN MODEL
# ==========================================

print("\nTraining Random Forest...")

model.fit(X_train, y_train)

print("Training complete!")


# ==========================================
# 6. MAKE PREDICTIONS
# ==========================================

y_pred = model.predict(X_test)


# ==========================================
# 7. EVALUATE MODEL
# ==========================================

accuracy = accuracy_score(y_test, y_pred)

precision = precision_score(
    y_test,
    y_pred,
    pos_label=0
)

recall = recall_score(
    y_test,
    y_pred,
    pos_label=0
)

f1 = f1_score(
    y_test,
    y_pred,
    pos_label=0
)

print("\n===== MODEL RESULTS =====")

print(f"Accuracy:  {accuracy:.4f}")
print(f"Precision: {precision:.4f}")
print(f"Recall:    {recall:.4f}")
print(f"F1 Score:  {f1:.4f}")


# ==========================================
# 8. CLASSIFICATION REPORT
# ==========================================

print("\n===== CLASSIFICATION REPORT =====")

print(
    classification_report(
        y_test,
        y_pred,
        target_names=["Phishing", "Legitimate"]
    )
)


# ==========================================
# 9. CONFUSION MATRIX
# ==========================================

print("\n===== CONFUSION MATRIX =====")

print(confusion_matrix(y_test, y_pred))


# ==========================================
# 10. FEATURE IMPORTANCE
# ==========================================

importance = pd.DataFrame({
    "Feature": feature_columns,
    "Importance": model.feature_importances_
})

importance = importance.sort_values(
    by="Importance",
    ascending=False
)

print("\n===== FEATURE IMPORTANCE =====")

print(importance.to_string(index=False))


# ==========================================
# 11. SAVE MODEL
# ==========================================

model_path = "model/phishing_model.joblib"

joblib.dump(model, model_path)

print(f"\nModel saved to: {model_path}")
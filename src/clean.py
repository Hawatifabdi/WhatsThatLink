import pandas as pd
from pathlib import Path

from features import extract_features, FEATURE_COLUMNS


INPUT_PATH = Path("data/phiusiil.csv")
OUTPUT_PATH = Path("data/processed_urls.csv")


print("Loading PhiUSIIL dataset...")

df = pd.read_csv(INPUT_PATH)

print(f"Original dataset: {df.shape}")


# -----------------------------------------
# Generate features from the RAW URLs
# -----------------------------------------

print("\nExtracting URL features...")

feature_rows = []

for i, url in enumerate(df["URL"]):

    if i % 10000 == 0:
        print(f"Processed {i:,} / {len(df):,}")

    features = extract_features(url)

    feature_rows.append(
        [features[column] for column in FEATURE_COLUMNS]
    )


# -----------------------------------------
# Create feature dataframe
# -----------------------------------------

X = pd.DataFrame(
    feature_rows,
    columns=FEATURE_COLUMNS
)

y = df["label"].reset_index(drop=True)


processed_df = X.copy()

processed_df["label"] = y


# -----------------------------------------
# Check dataset
# -----------------------------------------

print("\nProcessed dataset shape:")
print(processed_df.shape)

print("\nMissing values:")
print(processed_df.isnull().sum().sum())

print("\nLabel distribution:")
print(processed_df["label"].value_counts())


# -----------------------------------------
# Save
# -----------------------------------------

processed_df.to_csv(
    OUTPUT_PATH,
    index=False
)

print(f"\nSaved processed dataset to:")
print(OUTPUT_PATH)
import pandas as pd

# Load dataset
df = pd.read_csv("data/phiusiil.csv")

print("\n===== DATASET SHAPE =====")
print(df.shape)

print("\n===== COLUMNS =====")
print(df.columns.tolist())

print("\n===== FIRST 5 ROWS =====")
print(df.head())

print("\n===== DATA TYPES =====")
print(df.dtypes)

print("\n===== MISSING VALUES =====")
print(df.isnull().sum())

print("\n===== DUPLICATES =====")
print(df.duplicated().sum())

print("\n===== DATASET INFO =====")
print(df.info())

print("\n===== LABEL DISTRIBUTION =====")
# We'll identify the actual label column after seeing the output.

print("\n===== LABEL DISTRIBUTION =====")
print(df["label"].value_counts())

print("\n===== LABEL PERCENTAGES =====")
print(df["label"].value_counts(normalize=True) * 100)
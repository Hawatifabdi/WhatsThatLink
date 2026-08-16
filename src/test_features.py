from features import extract_features, FEATURE_COLUMNS


test_url = "https://example.com/login?id=12345"

features = extract_features(test_url)

print("\nExtracted features:\n")

for name in FEATURE_COLUMNS:
    print(f"{name}: {features[name]}")
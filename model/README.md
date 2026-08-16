# Model artifact

`phishing_model.joblib` is generated locally and deliberately excluded from Git.

Before running the API after cloning, train the model with `python src/train.py`
or place a trusted copy of the artifact in this directory. Only load model files
you created or obtained from a trusted source: joblib files can execute code
while being loaded.

from urllib.parse import urlparse
import ipaddress
import re


FEATURE_COLUMNS = [
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



def extract_features(url):
    """
    Extract URL-based features from a URL.

    Returns:
        dict containing the 18 features used by the ML model.
    """

    # Make sure the URL has a scheme
    if not re.match(r"^[a-zA-Z][a-zA-Z0-9+.-]*://", url):
        url = "http://" + url

    parsed = urlparse(url)

    # -----------------------------
    # Basic URL information
    # -----------------------------

    full_url = url
    domain = parsed.hostname or ""

    url_length = len(full_url)
    domain_length = len(domain)

    # -----------------------------
    # IP address detection
    # -----------------------------

    is_domain_ip = 0

    try:
        ipaddress.ip_address(domain)
        is_domain_ip = 1
    except ValueError:
        pass

    # -----------------------------
    # TLD
    # -----------------------------

    tld = ""

    if "." in domain:
        tld = domain.split(".")[-1]

    tld_length = len(tld)

    # -----------------------------
    # Subdomains
    # -----------------------------

    domain_parts = domain.split(".")

    no_of_subdomain = max(len(domain_parts) - 2, 0)

    # -----------------------------
    # Obfuscation
    # -----------------------------

    suspicious_patterns = [
        "%",
        "\\x",
        "%2f",
        "%3a",
        "%40",
        "%2e",
        "0x"
    ]

    lower_url = full_url.lower()

    no_of_obfuscated_char = 0

    for pattern in suspicious_patterns:
        no_of_obfuscated_char += lower_url.count(pattern)

    has_obfuscation = int(no_of_obfuscated_char > 0)

    obfuscation_ratio = (
        no_of_obfuscated_char / url_length
        if url_length > 0
        else 0
    )

    # -----------------------------
    # Character statistics
    # -----------------------------

    no_of_letters = sum(c.isalpha() for c in full_url)

    no_of_digits = sum(c.isdigit() for c in full_url)

    letter_ratio = (
        no_of_letters / url_length
        if url_length > 0
        else 0
    )

    digit_ratio = (
        no_of_digits / url_length
        if url_length > 0
        else 0
    )

    # -----------------------------
    # Special characters
    # -----------------------------

    no_of_equals = full_url.count("=")
    no_of_qmark = full_url.count("?")
    no_of_ampersand = full_url.count("&")

    common_special_chars = set(
        "=?&"
    )

    no_of_other_special_chars = sum(
        not c.isalnum() and c not in common_special_chars
        for c in full_url
    )

    special_char_ratio = (
        no_of_other_special_chars / url_length
        if url_length > 0
        else 0
    )

    # -----------------------------
    # HTTPS
    # -----------------------------

    is_https = int(parsed.scheme.lower() == "https")

    # -----------------------------
    # Return features
    # -----------------------------

    return {
        "URLLength": url_length,
        "DomainLength": domain_length,
        "IsDomainIP": is_domain_ip,
        "TLDLength": tld_length,
        "NoOfSubDomain": no_of_subdomain,
        "HasObfuscation": has_obfuscation,
        "NoOfObfuscatedChar": no_of_obfuscated_char,
        "ObfuscationRatio": obfuscation_ratio,
        "NoOfLettersInURL": no_of_letters,
        "LetterRatioInURL": letter_ratio,
        "NoOfDegitsInURL": no_of_digits,
        "DegitRatioInURL": digit_ratio,
        "NoOfEqualsInURL": no_of_equals,
        "NoOfQMarkInURL": no_of_qmark,
        "NoOfAmpersandInURL": no_of_ampersand,
        "NoOfOtherSpecialCharsInURL": no_of_other_special_chars,
        "SpacialCharRatioInURL": special_char_ratio,
        "IsHTTPS": is_https
    }
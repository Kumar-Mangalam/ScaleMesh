#!/usr/bin/env python3
"""
ScaleMesh Failure Resilience & Fault Tolerance Verification Test
Tests API Gateway degradation handling, bad input sanitization, and service down handling.
"""

import argparse
import json
import urllib.request
import urllib.error

def test_endpoint(name, url, method="GET", payload=None, expected_status=200):
    print(f"[*] Testing: {name} -> {method} {url}")
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(
        url,
        data=data,
        headers={'Content-Type': 'application/json', 'User-Agent': 'ScaleMesh-ResilienceTester/1.0'},
        method=method
    )
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            status = resp.getcode()
            body = resp.read().decode('utf-8')
            passed = (status == expected_status)
            print(f"    Status: {status} (Expected: {expected_status}) -> {'PASS' if passed else 'FAIL'}")
            return passed
    except urllib.error.HTTPError as e:
        passed = (e.code == expected_status)
        print(f"    HTTP Error: {e.code} (Expected: {expected_status}) -> {'PASS' if passed else 'FAIL'}")
        return passed
    except Exception as e:
        print(f"    Connection / Network Error: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description="Test ScaleMesh Fault Tolerance & Validation")
    parser.add_argument("--gateway", default="http://localhost:8081", help="API Gateway URL")
    args = parser.parse_args()

    tests = [
        ("Gateway System Health", f"{args.gateway}/api/system/health", "GET", None, 200),
        ("Invalid Trip Creation (Empty Body)", f"{args.gateway}/api/trips", "POST", {}, 400),
        ("Invalid Node Creation (Negative CPU)", f"{args.gateway}/api/nodes", "POST", {"name": "invalid", "cpuCores": -2, "memoryGb": 0}, 400),
        ("Non-existent Trip Lookup", f"{args.gateway}/api/trips/TRIP-DOES-NOT-EXIST", "GET", None, 404),
        ("Non-existent Route Calculation", f"{args.gateway}/api/routes/calculate", "POST", {"originLat": 999.0, "originLng": 999.0, "destLat": 0.0, "destLng": 0.0}, 400)
    ]

    total = len(tests)
    passed = 0
    for name, url, method, payload, expected in tests:
        if test_endpoint(name, url, method, payload, expected):
            passed += 1

    print(f"\n=======================================================")
    print(f"Resilience & Validation Test Summary: {passed}/{total} Passed")
    print(f"=======================================================\n")

if __name__ == "__main__":
    main()

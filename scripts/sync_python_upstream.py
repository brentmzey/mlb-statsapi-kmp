#!/usr/bin/env python3
"""
Automated Upstream Synchronization Script for mlb-statsapi-kmp.
Monitors upstream repository `toddrob99/MLB-StatsAPI`, analyzes endpoint changes,
validates parity with Kotlin Multiplatform MlbEndpoints.kt, and emits CI outputs.
"""
from __future__ import annotations

import ast
import json
import os
import re
import sys
import urllib.request
from typing import Any, Dict, List, Optional, Set


UPSTREAM_REPO: str = "toddrob99/MLB-StatsAPI"
GITHUB_API_COMMITS_URL: str = f"https://api.github.com/repos/{UPSTREAM_REPO}/commits/master"
UPSTREAM_ENDPOINTS_URL: str = f"https://raw.githubusercontent.com/{UPSTREAM_REPO}/master/statsapi/endpoints.py"
UPSTREAM_VERSION_URL: str = f"https://raw.githubusercontent.com/{UPSTREAM_REPO}/master/statsapi/version.py"


def fetch_json(url: str) -> Dict[str, Any]:
    """Fetches JSON data from a URL with standard User-Agent header."""
    headers: Dict[str, str] = {"User-Agent": "mlb-statsapi-kmp-sync-bot"}
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))


def fetch_text(url: str) -> str:
    """Fetches raw text data from a URL."""
    headers: Dict[str, str] = {"User-Agent": "mlb-statsapi-kmp-sync-bot"}
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=15) as resp:
        return resp.read().decode("utf-8")


def parse_python_endpoints_ast(endpoints_py_text: str) -> Set[str]:
    """Parses top-level endpoint dictionary keys from Python endpoints.py text using AST."""
    try:
        # Strip header markdown if any
        code_lines = [l for l in endpoints_py_text.splitlines() if not l.startswith("Title:") and not l.startswith("Description:") and not l.startswith("Source:") and not l.startswith("---")]
        cleaned_code = "\n".join(code_lines)
        tree = ast.parse(cleaned_code)
        for node in tree.body:
            if isinstance(node, ast.Assign):
                for target in node.targets:
                    if isinstance(target, ast.Name) and target.id == "ENDPOINTS" and isinstance(node.value, ast.Dict):
                        return {k.value for k in node.value.keys if isinstance(k, ast.Constant)}
    except Exception:
        pass
    # Fallback to top-level dict regex: '    "endpoint_name": {'
    pattern = re.compile(r'^\s{4}["\']([a-zA-Z0-9_]+)["\']\s*:\s*\{', re.MULTILINE)
    return set(pattern.findall(endpoints_py_text))


def parse_kotlin_endpoints(kotlin_endpoints_file: str) -> Set[str]:
    """Parses endpoint keys from Kotlin MlbEndpoints.kt file."""
    if not os.path.exists(kotlin_endpoints_file):
        return set()
    with open(kotlin_endpoints_file, "r", encoding="utf-8") as f:
        content = f.read()
    pattern = re.compile(r'["\']([a-zA-Z0-9_]+)["\']\s*to\s*["\']([^"\']+)["\']')
    return {match.group(1) for match in pattern.finditer(content)}


def main() -> None:
    print("==============================================================================")
    print(f" 🔍 Checking Upstream Python Repo: {UPSTREAM_REPO}")
    print("==============================================================================")

    script_dir = os.path.dirname(os.path.abspath(__file__))
    proj_dir = os.path.abspath(os.path.join(script_dir, ".."))
    kotlin_file = os.path.join(proj_dir, "src", "commonMain", "kotlin", "com", "sabermetrics", "statsapi", "MlbEndpoints.kt")

    # 1. Fetch upstream commit & version
    try:
        commit_data = fetch_json(GITHUB_API_COMMITS_URL)
        latest_sha: str = commit_data.get("sha", "unknown")[:8]
        commit_msg: str = commit_data.get("commit", {}).get("message", "").split("\n")[0]
        print(f"✅ Latest Upstream Commit: {latest_sha} - \"{commit_msg}\"")
    except Exception as e:
        print(f"⚠️ Could not fetch commit info: {e}")
        latest_sha = "unknown"

    try:
        version_text = fetch_text(UPSTREAM_VERSION_URL)
        version_match = re.search(r'VERSION\s*=\s*["\']([^"\']+)["\']', version_text)
        upstream_version: str = version_match.group(1) if version_match else "1.0.0"
        print(f"📦 Upstream Python Version: {upstream_version}")
    except Exception as e:
        print(f"⚠️ Could not fetch version info: {e}")
        upstream_version = "1.0.0"

    # 2. Fetch endpoints and check parity
    try:
        endpoints_text = fetch_text(UPSTREAM_ENDPOINTS_URL)
        python_endpoints = parse_python_endpoints_ast(endpoints_text)
        kotlin_endpoints = parse_kotlin_endpoints(kotlin_file)

        missing_in_kotlin = python_endpoints - kotlin_endpoints
        print(f"📊 Endpoint Parity: {len(kotlin_endpoints)} / {len(python_endpoints)} Upstream Endpoints Supported.")

        if missing_in_kotlin:
            print(f"⚠️ Found {len(missing_in_kotlin)} new endpoint(s) in upstream Python library:")
            for ep in missing_in_kotlin:
                print(f"   • {ep}")
            has_updates = "true"
        else:
            print("✅ 100% Endpoint Parity: Kotlin Multiplatform SDK is fully up to date with upstream Python repository!")
            has_updates = "false"

    except Exception as e:
        print(f"⚠️ Error verifying endpoints: {e}")
        has_updates = "false"

    # 3. Emit GitHub Actions Step Outputs
    github_output = os.getenv("GITHUB_OUTPUT")
    if github_output and os.path.exists(github_output):
        with open(github_output, "a", encoding="utf-8") as f:
            f.write(f"has_updates={has_updates}\n")
            f.write(f"upstream_version={upstream_version}\n")
            f.write(f"upstream_commit={latest_sha}\n")

    print("==============================================================================")


if __name__ == "__main__":
    main()

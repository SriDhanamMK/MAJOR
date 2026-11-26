
import pandas as pd
import pathlib
import requests
import time

BASE = pathlib.Path("external_benchmark/SSCD/dataset-01")
OUT = pathlib.Path("results")
OUT.mkdir(exist_ok=True)

URL = "http://localhost:8080/api/compare"
THRESHOLD = 89.03

session = requests.Session()
results = []

for filename, language in [
    ("dataset_c.csv", "c"),
    ("dataset_cpp.csv", "cpp")
]:
    df = pd.read_csv(BASE / filename)

    for index, r in df.iterrows():
        row = {
            "language": language,
            "granularity": r["granularity"],
            "pair": index + 1,
            "score": None,
            "detected": False,
            "parser_ok": False,
            "error": ""
        }

        try:
            a_path = BASE / r["private_path"]
            b_path = BASE / r["os_path"]

            a = a_path.read_text(
                encoding="utf-8", errors="replace"
            ).splitlines()[
                int(r["private_start_line"]) - 1:
                int(r["private_end_line"])
            ]

            b = b_path.read_text(
                encoding="utf-8", errors="replace"
            ).splitlines()[
                int(r["os_path_start_line"]) - 1:
                int(r["os_path_end_line"])
            ]

            response = session.post(
                URL,
                json={
                    "language": language,
                    "sourceA": "\n".join(a),
                    "sourceB": "\n".join(b),
                    "enableLiteralAbstraction": False,
                    "enableReordering": True
                },
                timeout=180
            )
            response.raise_for_status()
            data = response.json()

            warnings = " ".join(data.get("warnings") or [])
            diag_a = data.get("diagnosticsA") or []
            diag_b = data.get("diagnosticsB") or []

            parser_ok = (
                not diag_a and not diag_b
                and "syntax errors" not in warnings.lower()
            )

            score = data.get("similarityScore")
            row["score"] = score
            row["parser_ok"] = parser_ok

            if parser_ok and score is not None:
                row["detected"] = score >= THRESHOLD

        except Exception as e:
            row["error"] = str(e)

        results.append(row)

        if len(results) % 10 == 0:
            print("Processed:", len(results), "/ 180", flush=True)

        time.sleep(0.05)

out = pd.DataFrame(results)
out.to_csv(OUT / "SSCD_Detailed_Results.csv", index=False)

print("\n===== SSCD BENCHMARK RESULTS =====")

for keys, group in out.groupby(["language", "granularity"]):
    valid = group[group["parser_ok"]]
    detected = int(valid["detected"].sum())

    print(
        keys,
        "Total:", len(group),
        "Parsed:", len(valid),
        "Detected:", detected,
        "Recall on parsed:", round(
            detected / len(valid) * 100, 2
        ) if len(valid) else "N/A"
    )

valid = out[out["parser_ok"]]
detected = int(valid["detected"].sum())

print("\nTOTAL PAIRS:", len(out))
print("PARSER SUCCESS:", len(valid))
print("DETECTED:", detected)
print("RECALL ON PARSED (%):", round(
    detected / len(valid) * 100, 2
) if len(valid) else "N/A")
print("END-TO-END DETECTION (%):", round(
    detected / len(out) * 100, 2
))
print("PARSER FAILURES:", len(out) - len(valid))
print("ERRORS:", (out["error"] != "").sum())
print("THRESHOLD:", THRESHOLD)
print("SAVED: results/SSCD_Detailed_Results.csv")

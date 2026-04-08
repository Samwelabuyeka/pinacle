#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from integrations.vendor_registry import vendor_registry_as_dict


def main() -> None:
    target = ROOT / "config" / "maya_vendor_manifest.json"
    target.write_text(json.dumps(vendor_registry_as_dict(), indent=2))
    print(target)


if __name__ == "__main__":
    main()

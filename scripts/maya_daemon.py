#!/usr/bin/env python3
import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from maya_core.organism import MayaOrganism


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--once', action='store_true', help='Run one organism cycle and exit')
    p.add_argument('--interval', type=int, default=10, help='Seconds between cycles')
    args = p.parse_args()

    org = MayaOrganism()
    if args.once:
        done = org.step()
        print(f"processed_tasks={len(done)}")
    else:
        print('Maya organism running... Ctrl+C to stop')
        org.run_forever(interval_sec=args.interval)


if __name__ == '__main__':
    main()

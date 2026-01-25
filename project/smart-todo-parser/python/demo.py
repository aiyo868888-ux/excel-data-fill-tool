#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Simple demo without Chinese character display issues"""
import sys
sys.path.insert(0, '.')

from smart_todo_parser import TodoParser

def main():
    parser = TodoParser()

    examples = [
        "3pm meeting tomorrow",
        "Submit report by end of month",
        "Job interview in 3 days",
        "Weekend party",
    ]

    print("=" * 50)
    print("Smart Todo Parser Demo")
    print("=" * 50)

    for text in examples:
        result = parser.parse(text)
        print(f"\nInput: {text}")
        print(f"  Task: {result['task']}")
        print(f"  Time: {result['datetime']}")
        print(f"  Type: {result['type']}")
        print(f"  Priority: {result['priority']}")

    print("\n" + "=" * 50)
    print("Demo completed!")
    print("=" * 50)

if __name__ == "__main__":
    main()

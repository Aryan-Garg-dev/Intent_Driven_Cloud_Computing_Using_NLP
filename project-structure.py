#!/usr/bin/env python3
"""
Project Structure Generator
Creates a visual directory tree of the Intent-Driven Cloud Computing project
"""

import os
from pathlib import Path

PROJECT_ROOT = "e:\\VIT Projects\\Intent_Driven_Cloud_Computing_Using_NLP"

def generate_tree(directory, prefix="", is_last=True, ignore_dirs={'.git', '.idea', 'target', '__pycache__'}):
    """Generate a visual tree of the directory structure"""
    
    try:
        entries = sorted(os.listdir(directory))
    except PermissionError:
        return
    
    # Filter ignored directories
    entries = [e for e in entries if e not in ignore_dirs and not e.startswith('.')]
    
    for i, entry in enumerate(entries):
        path = os.path.join(directory, entry)
        is_last_entry = (i == len(entries) - 1)
        
        # Print the entry
        connector = "└── " if is_last_entry else "├── "
        print(f"{prefix}{connector}{entry}{'/' if os.path.isdir(path) else ''}")
        
        # Recurse into directories
        if os.path.isdir(path) and entry not in ignore_dirs:
            extension = "    " if is_last_entry else "│   "
            generate_tree(path, prefix + extension, is_last_entry, ignore_dirs)

if __name__ == "__main__":
    print("\n" + "="*70)
    print("PROJECT STRUCTURE: Intent-Driven Cloud Computing Simulation")
    print("="*70 + "\n")
    
    print("project-root/")
    generate_tree(PROJECT_ROOT)
    
    print("\n" + "="*70)
    print("\nKEY FILES:")
    print("  - NaturalLanguageIntentParser.java    Enhanced NLP engine")
    print("  - SimulationUI.java                   Main JavaFX application")
    print("  - *Panel.java                         UI component panels")
    print("  - pom.xml                             Maven dependencies")
    print("  - run.bat / run.sh                    Build scripts")
    print("  - README.md                           Full documentation")
    print("  - QUICKSTART.md                       Getting started guide")
    print("\n" + "="*70 + "\n")

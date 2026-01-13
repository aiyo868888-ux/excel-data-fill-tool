"""
Runtime hook to fix numpy source directory detection in PyInstaller
Must be loaded before numpy is imported
"""
import sys
import os

# Monkey-patch the function that numpy uses to detect source directory
def _disable_numpy_source_check():
    """Disable numpy's source directory check"""
    # This must be done before numpy is imported
    try:
        # Remove current directory from sys.path to avoid numpy detection
        if '' in sys.path:
            sys.path.remove('')
    except:
        pass

_disable_numpy_source_check()

#!/bin/bash
# Update iOS BuildInfo.swift with current git hash
HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
DATE=$(date +%Y-%m-%d)
cat > ios/Hexgram/Models/BuildInfo.swift << EOF
import Foundation

enum BuildInfo {
    // Updated by build script: scripts/update-build-info.sh
    static let gitHash = "$HASH"
    static let buildDate = "$DATE"
}
EOF
echo "Updated BuildInfo: $HASH ($DATE)"

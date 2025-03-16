#!/bin/bash
set -x

cd "$(dirname "$0")"
ROOT_DIR=$(cd .. && pwd)
ZIP_FILE="$ROOT_DIR/jenkins-config.zip"
(cd "$ROOT_DIR" && zip -r "$(basename "$ZIP_FILE")" init-groovy-scripts multibranch-pipeline-jobs jcasc.yaml)
echo "Created $ZIP_FILE successfully!"

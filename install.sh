#!/bin/sh
set -e

REPO="ghstrider/scaladex-cli"
INSTALL_DIR="/usr/local/bin"
BINARY_NAME="scaladex-cli"

# Detect OS and architecture
OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
  Linux)
    case "$ARCH" in
      x86_64) ARTIFACT="scaladex-cli-linux-x86_64" ;;
      *) echo "Unsupported architecture: $ARCH"; exit 1 ;;
    esac
    ;;
  Darwin)
    case "$ARCH" in
      x86_64) ARTIFACT="scaladex-cli-macos-x86_64" ;;
      arm64)  ARTIFACT="scaladex-cli-macos-arm64" ;;
      *) echo "Unsupported architecture: $ARCH"; exit 1 ;;
    esac
    ;;
  *)
    echo "Unsupported OS: $OS"
    exit 1
    ;;
esac

# Get latest release tag
VERSION=$(curl -sL "https://api.github.com/repos/$REPO/releases/latest" | grep '"tag_name"' | head -1 | cut -d'"' -f4)
if [ -z "$VERSION" ]; then
  echo "Failed to fetch latest version"
  exit 1
fi

URL="https://github.com/$REPO/releases/download/$VERSION/$ARTIFACT"

echo "Installing $BINARY_NAME $VERSION ($OS $ARCH)..."
curl -sL "$URL" -o "/tmp/$BINARY_NAME"
chmod +x "/tmp/$BINARY_NAME"

if [ -w "$INSTALL_DIR" ]; then
  mv "/tmp/$BINARY_NAME" "$INSTALL_DIR/$BINARY_NAME"
else
  echo "Need sudo to install to $INSTALL_DIR"
  sudo mv "/tmp/$BINARY_NAME" "$INSTALL_DIR/$BINARY_NAME"
fi

echo "Installed $BINARY_NAME to $INSTALL_DIR/$BINARY_NAME"
echo "Run 'scaladex-cli search cats-core' to try it out"

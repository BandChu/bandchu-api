#!/usr/bin/env sh
# This file is sourced by Husky hooks.

if [ -z "$husky_skip_init" ]; then
  readonly hook_name="$(basename -- "$0")"
  echo "🐶 Husky Start - Hook: $hook_name"

  readonly husky_skip_init=1
  export husky_skip_init

  sh "$0" "$@"
  exit_code="$?"

  if [ $exit_code -ne 0 ]; then
    echo "❌ Husky Error - Hook: $hook_name (exit $exit_code)"
  fi

  echo "✅ Husky Success - Hook: $hook_name"
  exit $exit_code
fi
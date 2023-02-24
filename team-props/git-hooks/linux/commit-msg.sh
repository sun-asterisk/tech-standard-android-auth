#!/usr/bin/env bash
echo "Checking your commit message"

<<comment
 \b word
 \s space
 .* any chars
 ^, $: start, end
comment

commit_regex='^\[\b(add|modify|fix|revert|hotfix)\b\]\s.+$'
error_msg="Aborting commit. Your commit message does not follow the commit message rule"

if ! grep -iqE "$commit_regex" "$1"; then
    echo "$error_msg" >&2
    exit 1
fi

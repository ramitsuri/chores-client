prev_tag=$(git describe --tags "$(git rev-list --tags --max-count=1)")
# Remove 'v' from tag which is in format v6.6 to get version name
old_version_name="${prev_tag/v/}"
# Remove '.' from version name to get version code
old_version_code="${old_version_name//.}"
# Add 1 to get new version code
new_version_code="$((old_version_code + 1))"
new_version_code_length=${#new_version_code}
# Add back '.' by printing chars from index 0 for (length - 1) then '.' and then the last char
new_version_name="${new_version_code:0:$((new_version_code_length-1))}.${new_version_code: -1}"

# Replace old version code line with new version code
sed -i '' "s/versionCode = $old_version_code/versionCode = $new_version_code/g" ./android/build.gradle.kts
# Replace old version name line with new version name
sed -i '' "s/versionName = \"$old_version_name\"/versionName = \"$new_version_name\"/g" ./android/build.gradle.kts

git add -A
git commit -am "Release $new_version_name"
git push
git tag -a "v$new_version_name" -m "Tagging v$new_version_name"
git push origin "v$new_version_name"

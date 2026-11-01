Write-Host "Pushing changes..."
git add .
git commit -m "update: $(Get-Date -Format 'yyyy-MM-dd-HHmm')"
git push
$remote = git remote get-url origin
$url = $remote -replace 'git@github.com:', 'https://github.com/' -replace '\.git$', ''
Write-Host "Pushed. APK builds in ~8 min."
Write-Host "Watch: $url/actions"

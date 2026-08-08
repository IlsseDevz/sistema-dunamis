# Deploy — Sistema Dunamis (Render + MySQL)
#
# Uso:
#   1. Preencha as variáveis abaixo
#   2. Execute: .\scripts\deploy.ps1
#
# Requisitos: conta GitHub, conta Render, MySQL externo (Railway recomendado)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $ProjectRoot

# --- CONFIGURE AQUI ---
$GitHubUser = "SEU_USUARIO_GITHUB"
$RepoName = "sistema-dunamis"
$RenderServiceName = "sistema-dunamis"

# MySQL (Railway / PlanetScale / Aiven)
$DbHost = "HOST_MYSQL"
$DbPort = "3306"
$DbName = "dunamis_db"
$DbUser = "UTILIZADOR_MYSQL"
$DbPassword = "PASSWORD_MYSQL"

# Admin inicial (produção)
$AdminContacto = "900000001"
$AdminPassword = "AltereEstaPassword123!"
# ----------------------

$DbUrl = "jdbc:mysql://${DbHost}:${DbPort}/${DbName}?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"

Write-Host "==> Verificar git..." -ForegroundColor Cyan
if (-not (Test-Path ".git")) { throw "Repositório git não encontrado." }
$branch = git rev-parse --abbrev-ref HEAD
Write-Host "Branch: $branch"

$gh = Get-ChildItem "$env:USERPROFILE\tools\gh\*\bin\gh.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $gh) { $gh = Get-Command gh -ErrorAction SilentlyContinue }

if ($gh) {
    Write-Host "==> GitHub CLI encontrado" -ForegroundColor Cyan
    & $gh auth status 2>&1
    $remote = git remote get-url origin 2>$null
    if (-not $remote) {
        Write-Host "==> Criar repositório GitHub..." -ForegroundColor Cyan
        & $gh repo create "$GitHubUser/$RepoName" --public --source=. --remote=origin --push
    } else {
        Write-Host "==> Push para origin..." -ForegroundColor Cyan
        git push -u origin $branch
    }
} else {
    Write-Host "GitHub CLI não instalado. Crie manualmente:" -ForegroundColor Yellow
    Write-Host "  https://github.com/new?name=$RepoName"
    Write-Host "  git remote add origin https://github.com/$GitHubUser/$RepoName.git"
    Write-Host "  git push -u origin $branch"
}

Write-Host ""
Write-Host "==> Variáveis para o Render (Environment):" -ForegroundColor Green
@{
    "SPRING_PROFILES_ACTIVE" = "prod"
    "JPA_DDL_AUTO" = "update"
    "DB_URL" = $DbUrl
    "DB_USERNAME" = $DbUser
    "DB_PASSWORD" = $DbPassword
    "ADMIN_CONTACTO" = $AdminContacto
    "ADMIN_PASSWORD" = $AdminPassword
    "ADMIN_NAME" = "Administrador"
}.GetEnumerator() | Sort-Object Name | ForEach-Object { Write-Host ("  {0}={1}" -f $_.Key, $_.Value) }

Write-Host ""
Write-Host "==> Render — passos:" -ForegroundColor Green
Write-Host "  1. https://dashboard.render.com/blueprints"
Write-Host "  2. New Blueprint Instance -> ligar repo $RepoName"
Write-Host "  3. Colar as variáveis acima quando solicitado"
Write-Host "  4. Health check: /health"
Write-Host "  5. Após deploy: https://${RenderServiceName}.onrender.com/health"

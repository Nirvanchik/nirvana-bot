@echo off
chcp 866
:: I use CMD variable to pass bot app to avoid hell with
:: partiall arguments pass in the underlying script.
set BOT_APP=nirvana-bot
runbot config_ru.xml
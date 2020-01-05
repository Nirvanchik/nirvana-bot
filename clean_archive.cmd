@echo off
chcp 866
:: I use CMD variable to pass bot app to avoid hell with
:: partiall arguments pass in the underlying script.
set BOT_APP=clean-archive-bot
runbot config_clean_archive.xml

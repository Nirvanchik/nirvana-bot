@echo off
chcp 1251
:: I use CMD variable to pass bot app to avoid hell with
:: partiall arguments pass in the underlying script.
set BOT_APP=archive-tools
runbot %1 %2 %3
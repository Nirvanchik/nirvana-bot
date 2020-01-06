@echo off
chcp 866
:: I use CMD variable to pass bot app to avoid hell with
:: partiall arguments pass in the underlying script.
set BOT_APP=list-tools
runbot %1 %2 %3 %4 %5 %6 %7 %8 %9

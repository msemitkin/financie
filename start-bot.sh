cd /root/financie
# Send SIGTERM to the existing instance
pkill -f FinancieBot
# Wait for the process to fully terminate
while pgrep -f FinancieBot > /dev/null; do
echo "Waiting for FinancieBot to terminate..."
sleep 1
done
echo "FinancieBot terminated. Starting new instance."
nohup java -jar financie-1.3.1.jar -a FinancieBot > /dev/null 2>&1 &

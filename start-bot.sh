cd /root/financie
# Send SIGTERM to the existing instance
pkill -f FinancieBot
# Wait for the process to fully terminate
while pgrep -f FinancieBot > /dev/null; do
echo "Waiting for FinancieBot to terminate..."
sleep 1
done
echo "FinancieBot terminated. Starting new instance."

nohup java -jar financie.jar -a FinancieBot > /dev/null 2>&1 &

timeout=${READINESS_TIMEOUT:-60}
echo "Waiting for FinancieBot to start for $timeout seconds..."
while [ !  -f ./ready ]; do
if [ "$timeout" -eq 0 ]; then
echo "FinancieBot failed to start within $timeout seconds."
exit 1
fi
sleep 1
timeout=$((timeout-1))
done
echo "FinancieBot started."

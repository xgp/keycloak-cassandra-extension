#!/bin/bash

echo "Sleeping for 1 minute!"
sleep 60

# run the keycloak entrypoint with the given params
RUN_OPTS="$@"
/opt/keycloak/bin/kc.sh $RUN_OPTS

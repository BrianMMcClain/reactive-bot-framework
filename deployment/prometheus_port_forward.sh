#!/bin/bash

kubectl port-forward --namespace default svc/prometheus-prometheus-oper-prometheus 9090:9090
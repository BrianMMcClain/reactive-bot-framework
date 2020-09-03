#!/bin/bash

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

helm install prometheus bitnami/prometheus-operator
helm install grafana bitnami/grafana
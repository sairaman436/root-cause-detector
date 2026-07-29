# Java Shared Library

## Purpose

Contains stable Java contracts and infrastructure-oriented helpers shared by Java modules.

## Why It Exists

The monorepo needs a governed place for shared Java assets without allowing direct cross-module coupling.

## Architecture Fit

This package supports the approved dependency rules. Business logic must remain in owning backend modules, not in this shared package.

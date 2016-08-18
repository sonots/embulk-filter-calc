# Calc filter plugin for Embulk

A filter plugin for Embulk to calculate with formula.

## Overview

* **Plugin type**: filter

## Configuration

- **columns**: columns to calculate
-   **name**: name of column to output calculation result.
-   **formula**: calculation formula.

## Example

```yaml
filters:
  - type: calc
    columns:
    - name: id
      formula: "1 + id"
    - name: account
      formula: "0.5 + account"
```

## Supported formula

* Basic Operators
  * Add: +
  * Sub: -
  * Mod: *
  * Div: /
  * Mod: %
  * Power: ^
  * Paren: ()
* Math functions
  * sin/cos/tan

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

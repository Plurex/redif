# Redif

A Redis Facade. Provides easily interchangeable implementations/clients:

- `RedifLettuce` - a wrapper around `io.lettuce:lettuce-core`. This provides a non-blocking client to Redis.
- `RedifInMemory` - an in memory implementation of Redis; No instance of Redis required. Great for testing, or for
  single instance in memory caching.

## Supported Commands

Command implementation is very limited. We will add more commands as we need more.

### Keys

- `pexpire`
- `del`

### Strings

- `set`
- `psetx`
- `setnx`
- `get`


package io.plurex.redif

import io.plurex.redif.commands.keys.KeysAPI
import io.plurex.redif.commands.strings.StringsAPI

interface RedifAPI :
    StringsAPI,
    KeysAPI
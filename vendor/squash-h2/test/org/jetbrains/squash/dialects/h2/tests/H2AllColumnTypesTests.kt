package org.jetbrains.squash.dialects.h2.tests

import org.jetbrains.squash.tests.*

class H2AllColumnTypesTests : AllColumnTypesTests(), DatabaseTests by H2DatabaseTests()
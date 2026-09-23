#!/bin/sh

#
# Taku_Lm_Adapter Gradle wrapper script (Gradle 7.3.3).
#
# Attempts to set APP_HOME to the parent of the script directory, then
# delegates to the GradleWrapperMain bundled in gradle/wrapper/gradle-wrapper.jar.
#

# Resolve links: $0 may be a link
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  case "$link" in
    /*) PRG="$link" ;;
    *)  PRG=`dirname "$PRG"`/"$link" ;;
  esac
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_BASE_NAME=`basename "$0"`

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"

# Default JVM options
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

MAX_FD=maximum

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  MINGW* ) msys=true ;;
  DARWIN* ) darwin=true ;;
esac

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi
if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine lower/normal env var handling
# Use explicit classpath
exec "$JAVACMD" $DEFAULT_JVM_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
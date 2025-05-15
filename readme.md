# CORBA Server and Client Setup Guide

This guide outlines the steps for setting up and running a CORBA server and client using IntelliJ IDEA.

## Prerequisites
- Java Development Kit (JDK)
- Apache Ant (for building)
- CORBA libraries (including `tnameserv` and other necessary libraries)
- IntelliJ IDEA installed

## Project Structure
The project includes:
1. **Server**: A Java application that provides a CORBA service.
2. **Client**: A Java application that calls the CORBA service.
3. **Name Server (`tnameserv`)**: A CORBA naming service for resolving object references.

## Running the Server and Client on Localhost

### Step 1: Start the Name Server
CORBA uses a **name server** to resolve object references. To start it:
1. Open a terminal.
2. Run the following command:
   ```sh
   tnameserv -ORBInitialPort <Port>
This starts the Name Server on port 1050. Make sure the port is not blocked by firewalls or already in use.

### Step 2: Run the CORBA Server
 Edit the configurations and add VM Options. 
add this:
```
   -Dorg.omg.CORBA.ORBInitialPort=<Port> -Dorg.omg.CORBA.ORBInitialHost=<IP Address>

```
### Step 3: Run the CORBA Client
    Do the same in step 2


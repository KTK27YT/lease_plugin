```
                                _
                                | |    ___  __ _ ___  ___
                                | |   / _ \/ _` / __|/ _ \
                                | |__|  __/ (_| \__ \  __/
                                |_____\___|\__,_|___/\___|
                                ------------------------------------------------
                                Minecraft plugin for leasing commands to players
```
<p align="center">
<img src="https://img.shields.io/badge/License-MIT-yellow.svg"></img>
</p>

Is it me or do you also want a plugin where you want to give players a temporary permission? <br>
*giving players creative mode, op for a brief period. Like having a temp admin take over for you*<br>

This plugin allows you to do just that! <br>


# Installation:

**For server owners:** <br>
download the plugin from the [releases](https://github.com/KTK27YT/lease_plugin/releases) tab, and put it in your plugins folder. <br>
you can also go to the [Spigot Page](https://www.spigotmc.org/resources/lease.114517/) and view it from there

**For developers:** <br>
clone the repository, and make sure you add Bukkit as a dependecy.



# Usage:

Command Usage:

To grant leases:
```cmd
/lease <lease_name> <player_to_give_lease> <duration (in minutes)>
```

to view active leases:
```cmd
/activelease
```


# Config:
To properly make use of this plugin, there's a lease.yml file that gets created in the your_server/plugins/Lease folder. <br>
Here's how the config looks like:
```yml
leases:
  creative:
    start_cmd: "gamemode creative %player%"
    end_cmd: "gamemode survival %player%"

  op:
    start_cmd: "op %player%"
    end_cmd: "deop %player%"

  spectator:
    start_cmd: "gamemode spectator %player%"
    end_cmd: "gamemode survival %player%"
```
You can add as many leases as you want, and you can also edit the existing ones. <br>
To create a new lease you want to:
```yml
name_of_your_lease:
    start_cmd: "command_to_execute_when_lease_starts"
    end_cmd: "command_to_execute_when_lease_ends"
```
**Note:** make sure to add %player% where a players name is mentioned <br> For example:
when running the creative lease on John:
```yml
/gamemode creative %player%
/gamemode creative John
```

and

```yml
start_cmd: What command to run when the lease is granted?
end_cmd: What command to run when the lease is expired?
```

# Permissions

There are 2 permissions that come with this plugin:

```yml
lease.lease - allows the player to grant leases
lease.activelease - allows the player to see active leases
```
**Note**: these 2 permissions are granted to OP users by default.

# Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Also explain what changes you have made, so I can learn from it too!

# License
[MIT](https://choosealicense.com/licenses/mit/)

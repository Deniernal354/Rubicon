/*
 * Copyright (c) 2018  Rubicon Bot Development Team
 * Licensed under the GPL-3.0 license.
 * The full license text is available in the LICENSE file provided with this project.
 */

package fun.rubicon.listener;

import fun.rubicon.commands.moderation.CommandMute;

/**
 * @author Yannick Seeger / ForYaSee
 */
public class AllShardsLoadedEvent {

    public AllShardsLoadedEvent() {
        call();
    }

    private void call() {
        CommandMute.loadMutes();
    }
}
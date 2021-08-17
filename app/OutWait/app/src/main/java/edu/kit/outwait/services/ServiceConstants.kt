package edu.kit.outwait.services

/**
 * Constants with default values, used for the configuration of the NotificationChannels
 * in the services package
 */
const val PERM_CHANNEL_ID = "permChannel_ID"
const val PERM_CHANNEL_NAME = "Permanent_Info_Channel"
const val PERM_CHANNEL_DESCRIPTION = "Displays information regarding next appointment"

const val SECOND_CHANNEL_ID = "secondChannel_ID"
const val SECOND_CHANNEL_NAME = "Second_Info_Channel"
const val SECOND_CHANNEL_DESCRIPTION = "Displays info and sudden changes regarding all appointments"

/**
 * Constants with default values, used for the configuration of the injected NotificationBuilders
 * in the services package
 */
const val PERM_NOTIFICATION_ID = 123
const val DELAY_NOTIFICATION_ID = 456
const val PENDING_NOTIFICATION_ID = 789

const val PERM_CHANNEL_DEFAULT_TITLE = "Your next appointment"
const val SECOND_CHANNEL_DEFAULT_TITLE = "Info regarding appointments"
const val NOTIFICATION_CHANNEL_DEFAULT_TEXT = "will be displayed here"

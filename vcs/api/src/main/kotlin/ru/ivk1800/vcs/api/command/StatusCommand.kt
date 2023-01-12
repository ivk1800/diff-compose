package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsStatus

/**
 * https://git-scm.com/docs/git-status
 */
abstract class StatusCommand : Command<VcsStatus>

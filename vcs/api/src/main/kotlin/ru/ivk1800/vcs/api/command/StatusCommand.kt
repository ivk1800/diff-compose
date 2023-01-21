package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsStatus

/**
 * https://git-scm.com/docs/git-status
 */
interface StatusCommand : Command<VcsStatus>

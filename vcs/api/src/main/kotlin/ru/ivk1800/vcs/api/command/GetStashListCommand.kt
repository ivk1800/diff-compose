package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsStash

interface GetStashListCommand : Command<List<VcsStash>>

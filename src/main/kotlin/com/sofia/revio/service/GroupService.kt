package com.sofia.revio.service

import com.sofia.revio.exception.ItemNotFoundException
import com.sofia.revio.exception.InactiveItemException
import com.sofia.revio.model.Group
import com.sofia.revio.model.request.GroupCreateRequest
import com.sofia.revio.model.request.RevisersUsernameRequest
import com.sofia.revio.model.request.toGroup
import com.sofia.revio.repository.GroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GroupService(
    private val groupRepository: GroupRepository,
) {

    fun createGroup(groupCreateRequest: GroupCreateRequest, username: String): Group {
        groupCreateRequest.creatorUsername = username
        val group = groupCreateRequest.toGroup()
        return groupRepository.save(group)
    }

    fun addToGroup(revisers: RevisersUsernameRequest, groupId: String): Group {
        val group = getGroup(groupId)

        if (group.active.not()) {
            throw InactiveItemException("Group now allowed to receive members. Current state is inactive")
        }

        group.users?.addAll(revisers.users)
        group.lastUpdatedAt = LocalDateTime.now()

        return groupRepository.save(group)
    }

    fun getGroup(groupId: String): Group {
        return groupRepository.findById(groupId).orElseThrow {
            throw ItemNotFoundException("Group not found")
        }
    }

    fun removeGroup(groupId: String): Group {
        val group = getGroup(groupId)

        if (group.active.not()) {
            throw InactiveItemException("Group current state is inactive")
        }
        group.active = false
        group.lastUpdatedAt = LocalDateTime.now()

        return groupRepository.save(group)
    }

    fun removeFromGroup(revisersList: RevisersUsernameRequest, groupId: String): Group? {
        val group = getGroup(groupId)

        if (group.active.not()) {
            throw InactiveItemException("Group current state is inactive")
        }
        group.users?.removeAll(revisersList.users.toSet())
        group.lastUpdatedAt = LocalDateTime.now()

        return groupRepository.save(group)
    }

    fun activateGroup(groupId: String): Group {
        val group = getGroup(groupId)

        if (group.active) {
            throw InactiveItemException("Group current state is active")
        }
        group.active = true
        group.lastUpdatedAt = LocalDateTime.now()

        return groupRepository.save(group)

    }
}
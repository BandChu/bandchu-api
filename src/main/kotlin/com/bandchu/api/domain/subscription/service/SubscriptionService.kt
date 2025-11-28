package com.bandchu.api.domain.subscription.service

import com.bandchu.api.domain.artist.repository.ArtiProfileRepository
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.subscription.dto.SubscriptionListItemResponse
import com.bandchu.api.domain.subscription.model.Subscription
import com.bandchu.api.domain.subscription.repository.SubscriptionRepository
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.util.getCurrentUserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val artiProfileRepository: ArtiProfileRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun subscribe(memberId: Long, artProfileId: Long): Subscription {
        // 역할 검증: FAN만 구독 가능
        val role = getCurrentUserRole()
        if (role != Role.FAN) {
            log.warn("Subscription attempt by non-FAN user. MemberId: $memberId, Role: $role")
            throw BusinessException(ErrorCode.INVALID_ROLE)
        }

        // 중복 구독 체크
        if (subscriptionRepository.existsByMemberIdAndArtProfileId(memberId, artProfileId)) {
            throw BusinessException(ErrorCode.SUBSCRIPTION_DUPLICATED)
        }

        // 구독 생성
        val subscription = Subscription(
            memberId = memberId,
            artProfileId = artProfileId
        )

        return subscriptionRepository.save(subscription)
    }

    fun unsubscribe(memberId: Long, artProfileId: Long) {
        // 구독 존재 확인 및 삭제
        val deleted = subscriptionRepository.deleteByMemberIdAndArtProfileId(memberId, artProfileId)
        if (!deleted) {
            throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }
    }

    fun getSubscriptions(memberId: Long): List<SubscriptionListItemResponse> {
        // 구독 목록 조회
        val subscriptions = subscriptionRepository.findByMemberId(memberId)

        // 아티스트 프로필 정보 조회
        return subscriptions.mapNotNull { subscription ->
            val artiProfile = artiProfileRepository.findById(subscription.artProfileId)
            if (artiProfile == null) {
                log.warn("ArtiProfile not found for subscription. MemberId: $memberId, ArtProfileId: ${subscription.artProfileId}")
                return@mapNotNull null
            }
            
            SubscriptionListItemResponse(
                artProfileId = subscription.artProfileId,
                artistName = artiProfile.artistName, 
                profileImage = artiProfile.profileImageUrl?.toString() ?: "" 
            )
        }
    }
}


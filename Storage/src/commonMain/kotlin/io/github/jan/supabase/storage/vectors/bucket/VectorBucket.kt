package io.github.jan.supabase.storage.vectors.bucket

import io.github.jan.supabase.serializer.UnixTimestampSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Vector bucket metadata
 * @property vectorBucketName Unique name of the vector bucket
 * @property creationTime Unix timestamp of when the bucket was created
 * @property encryptionConfiguration Optional encryption settings
 */
data class VectorBucket(
    val vectorBucketName: String,
    @Serializable(with = UnixTimestampSerializer::class) val creationTime: Instant? = null,
    val encryptionConfiguration: EncryptionConfiguration? = null
)

/**
 * Configuration for encryption at rest
 * @property kmsKeyArn ARN of the KMS key used for encryption
 * @property sseType Server-side encryption type (e.g., 'KMS')
 */
data class EncryptionConfiguration(
    val kmsKeyArn: String? = null,
    val sseType: String? = null
)

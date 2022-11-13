package com.ramitsuri.choresclient.network

import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ProgressStatusSerializer : KSerializer<ProgressStatus> {
    override val descriptor = PrimitiveSerialDescriptor("ProgressStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ProgressStatus {
        val progressStatus = try {
            ProgressStatus.fromKey(decoder.decodeString().toInt())
        } catch (e: Exception) {
            ProgressStatus.UNKNOWN
        }
        return progressStatus
    }

    override fun serialize(encoder: Encoder, value: ProgressStatus) {
        encoder.encodeInt(value.key)
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

object LocalDateTimeSerializer: KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}

object CreateTypeSerializer : KSerializer<CreateType> {
    override val descriptor = PrimitiveSerialDescriptor("CreateType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CreateType {
        val repeatType = try {
            CreateType.fromKey(decoder.decodeString().toInt())
        } catch (e: Exception) {
            CreateType.UNKNOWN
        }
        return repeatType
    }

    override fun serialize(encoder: Encoder, value: CreateType) {
        encoder.encodeInt(value.key)
    }
}

object RepeatUnitSerializer : KSerializer<RepeatUnit> {
    override val descriptor = PrimitiveSerialDescriptor("RepeatUnit", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): RepeatUnit {
        val repeatUnit = try {
            RepeatUnit.fromKey(decoder.decodeString().toInt())
        } catch (e: Exception) {
            RepeatUnit.NONE
        }
        return repeatUnit
    }

    override fun serialize(encoder: Encoder, value: RepeatUnit) {
        encoder.encodeInt(value.key)
    }
}

object ActiveStatusSerializer : KSerializer<ActiveStatus> {
    override val descriptor = PrimitiveSerialDescriptor("ActiveStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ActiveStatus {
        val repeatUnit = try {
            ActiveStatus.fromKey(decoder.decodeString().toInt())
        } catch (e: Exception) {
            ActiveStatus.UNKNOWN
        }
        return repeatUnit
    }

    override fun serialize(encoder: Encoder, value: ActiveStatus) {
        encoder.encodeInt(value.key)
    }
}

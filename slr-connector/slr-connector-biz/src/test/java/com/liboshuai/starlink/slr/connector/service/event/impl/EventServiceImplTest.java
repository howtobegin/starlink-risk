package com.liboshuai.starlink.slr.connector.service.event.impl;

import com.liboshuai.starlink.slr.connector.dao.kafka.provider.EventProvider;
import com.liboshuai.starlink.slr.connector.pojo.dto.event.KafkaEventGroupDTO;
import com.liboshuai.starlink.slr.connector.service.event.strategy.EventStrategy;
import com.liboshuai.starlink.slr.connector.service.event.strategy.EventStrategyHolder;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventStrategyHolder eventStrategyHolder;

    @Mock
    private EventProvider eventProvider;

    @InjectMocks
    private EventServiceImpl eventServiceImpl;

    private KafkaEventGroupDTO validKafkaEventGroupDTO;

    @BeforeEach
    public void setup() {
        // 初始化 eventAttribute 使用 Java 8 兼容的方式
        Map<String, String> eventAttribute = new HashMap<>();
        eventAttribute.put("attr1", "valueAttr1");

        KafkaEventDTO eventDTO = KafkaEventDTO.builder()
                .eventTime("2023-10-10 10:10:10")
                .keyCode("key1")
                .keyValue("value1")
                .eventCode("event1")
                .eventValue("value1")
                .eventAttribute(eventAttribute)
                .channel(ChannelEnum.GAME.getCode()) // 确保使用有效的 ChannelEnum 值
                .build();

        validKafkaEventGroupDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(Collections.singletonList(eventDTO))
                .build();
    }

    /**
     * 测试 validateUploadList 方法：渠道合法，事件列表非空且大小 <= 100
     */
    @Test
    public void testValidateUploadList_ValidInput() {
        // Arrange
        // 已在 setup() 方法中初始化 validKafkaEventGroupDTO

        // Mock EventStrategyHolder
        EventStrategy mockStrategy = mock(EventStrategy.class);
        when(eventStrategyHolder.getByChannel(anyString())).thenReturn(mockStrategy);

        // Mock EventProvider
        doNothing().when(eventProvider).batchSend(anyList());

        // Act & Assert
        assertThatCode(() -> eventServiceImpl.pushKafkaEvent(validKafkaEventGroupDTO))
                .doesNotThrowAnyException();

        // Verify interactions
        verify(eventStrategyHolder, times(1)).getByChannel(ChannelEnum.GAME.getCode());
        verify(eventProvider, times(1)).batchSend(anyList());
    }

    /**
     * 测试 validateUploadList 方法：渠道非法
     */
    @Test
    public void testValidateUploadList_InvalidChannel() {
        // Arrange
        KafkaEventGroupDTO invalidChannelDTO = KafkaEventGroupDTO.builder()
                .channel("INVALID_CHANNEL")
                .kafkaEventDTOList(validKafkaEventGroupDTO.getKafkaEventDTOList())
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(invalidChannelDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .hasMessageContaining("无效的渠道");

        // Verify that no further interactions occur
        verify(eventStrategyHolder, never()).getByChannel(anyString());
        verify(eventProvider, never()).batchSend(anyList());
    }

    /**
     * 测试 validateUploadList 方法：事件列表为空
     */
    @Test
    public void testValidateUploadList_EmptyKafkaEventDTOList() {
        // Arrange
        KafkaEventGroupDTO emptyListDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(new ArrayList<>()) // 使用新的空 ArrayList
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(emptyListDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .hasMessageContaining("事件数据集合不能为空");

        // Verify that no further interactions occur
        verify(eventStrategyHolder, never()).getByChannel(anyString());
        verify(eventProvider, never()).batchSend(anyList());
    }

    /**
     * 测试 validateUploadList 方法：事件列表大小超过 100
     */
    @Test
    public void testValidateUploadList_ExceedsMaxSize() {
        // Arrange
        List<KafkaEventDTO> largeList = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            // 初始化 eventAttribute 使用 Java 8 兼容的方式
            Map<String, String> eventAttribute = new HashMap<>();
            eventAttribute.put("attr" + i, "valueAttr" + i);

            KafkaEventDTO eventDTO = KafkaEventDTO.builder()
                    .eventTime("2023-10-10 10:10:10")
                    .keyCode("key" + i)
                    .keyValue("value" + i)
                    .eventCode("event" + i)
                    .eventValue("valueValue" + i)
                    .eventAttribute(eventAttribute)
                    .channel(ChannelEnum.GAME.getCode())
                    .build();
            largeList.add(eventDTO);
        }

        KafkaEventGroupDTO largeListDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(largeList)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(largeListDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .hasMessageContaining("元素个数必须小于等于 [100]");

        // Verify that no further interactions occur
        verify(eventStrategyHolder, never()).getByChannel(anyString());
        verify(eventProvider, never()).batchSend(anyList());
    }

    /**
     * 测试 checkAndFilter 方法：所有字段非空，无需过滤，正常推送
     */
    @Test
    public void testCheckAndFilter_AllValid() {
        // Arrange
        EventStrategy mockStrategy = mock(EventStrategy.class);
        when(eventStrategyHolder.getByChannel(anyString())).thenReturn(mockStrategy);
        doNothing().when(eventProvider).batchSend(anyList());

        // Act & Assert
        assertThatCode(() -> eventServiceImpl.pushKafkaEvent(validKafkaEventGroupDTO))
                .doesNotThrowAnyException();

        // Verify that batchSend was called with original list
        verify(eventProvider, times(1)).batchSend(validKafkaEventGroupDTO.getKafkaEventDTOList());
    }

    /**
     * 测试 checkAndFilter 方法：部分字段为空，需过滤并抛出异常
     */
    @Test
    public void testCheckAndFilter_SomeInvalid() {
        // Arrange
        // 初始化 eventAttribute 使用 Java 8 兼容的方式
        Map<String, String> validAttributes = new HashMap<>();
        validAttributes.put("attrValid", "valueValid");

        Map<String, String> invalidAttributes = new HashMap<>();

        // 创建有效和无效的事件
        KafkaEventDTO validEventDTO = validKafkaEventGroupDTO.getKafkaEventDTOList().get(0);

        KafkaEventDTO invalidEventDTO = KafkaEventDTO.builder()
                .eventTime("2023-10-10 10:10:10")
                .keyCode(null) // Invalid
                .keyValue("value")
                .eventCode("event")
                .eventValue("value")
                .eventAttribute(invalidAttributes)
                .channel(ChannelEnum.GAME.getCode())
                .build();

        KafkaEventGroupDTO groupDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(Arrays.asList(validEventDTO, invalidEventDTO))
                .build();

        EventStrategy mockStrategy = mock(EventStrategy.class);
        when(eventStrategyHolder.getByChannel(anyString())).thenReturn(mockStrategy);

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(groupDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .hasMessageContaining("keyCode必须非空");

        // Verify that batchSend was called only with the valid event
        verify(eventProvider, times(1)).batchSend(Mockito.argThat(list -> list.size() == 1));
    }

    /**
     * 测试 checkAndFilter 方法：所有字段为空，全部过滤并抛出异常
     */
    @Test
    public void testCheckAndFilter_AllInvalid() {
        // Arrange
        // 初始化 eventAttribute 使用 Java 8 兼容的方式
        KafkaEventDTO invalidEventDTO1 = KafkaEventDTO.builder()
                .eventTime("2023-10-10 10:10:10")
                .keyCode(null)
                .keyValue(null)
                .eventCode(null)
                .eventValue(null)
                .eventAttribute(null)
                .channel(ChannelEnum.GAME.getCode())
                .build();

        KafkaEventDTO invalidEventDTO2 = KafkaEventDTO.builder()
                .eventTime("2023-10-10 10:10:10")
                .keyCode("")
                .keyValue("")
                .eventCode("")
                .eventValue("")
                .eventAttribute(null)
                .channel(ChannelEnum.GAME.getCode())
                .build();

        KafkaEventGroupDTO groupDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(Arrays.asList(invalidEventDTO1, invalidEventDTO2))
                .build();

        EventStrategy mockStrategy = mock(EventStrategy.class);
        when(eventStrategyHolder.getByChannel(anyString())).thenReturn(mockStrategy);

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(groupDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .satisfies(ex -> {
                    // 断言包含所有错误信息
                    String message = ex.getMessage();
                    assertThat(message)
                            .contains("keyCode必须非空")
                            .contains("keyValue必须非空")
                            .contains("eventCode必须非空")
                            .contains("eventValue必须非空")
                            .contains("eventAttribute必须非空");
                });

        // Verify that batchSend was not called since all events are invalid
        verify(eventProvider, never()).batchSend(anyList());
    }

    /**
     * 测试 checkAndFilter 方法：kafkaEventDTOList 为空，不会调用 batchSend
     */
    @Test
    public void testCheckAndFilter_EmptyAfterFiltering() {
        // Arrange
        KafkaEventDTO invalidEventDTO = KafkaEventDTO.builder()
                .eventTime("2023-10-10 10:10:10")
                .keyCode(null)
                .keyValue(null)
                .eventCode(null)
                .eventValue(null)
                .eventAttribute(null)
                .channel(ChannelEnum.GAME.getCode())
                .build();

        KafkaEventGroupDTO groupDTO = KafkaEventGroupDTO.builder()
                .channel(ChannelEnum.GAME.getCode())
                .kafkaEventDTOList(Collections.singletonList(invalidEventDTO))
                .build();

        EventStrategy mockStrategy = mock(EventStrategy.class);
        when(eventStrategyHolder.getByChannel(anyString())).thenReturn(mockStrategy);

        // Act & Assert
        assertThatThrownBy(() -> eventServiceImpl.pushKafkaEvent(groupDTO))
                .isInstanceOf(RuntimeException.class) // 根据 ServiceExceptionUtil 实际返回的异常类型调整
                .satisfies(ex -> {
                    // 断言包含所有错误信息
                    String message = ex.getMessage();
                    assertThat(message)
                            .contains("keyCode必须非空")
                            .contains("keyValue必须非空")
                            .contains("eventCode必须非空")
                            .contains("eventValue必须非空")
                            .contains("eventAttribute必须非空");
                });

        // Verify that batchSend was not called since all events are invalid
        verify(eventProvider, never()).batchSend(anyList());
    }
}

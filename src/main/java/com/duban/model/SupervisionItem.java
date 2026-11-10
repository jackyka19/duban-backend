package com.duban.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisionItem {
    private Long id;
    private String name;           // 事項名稱
    private String startDate;      // 發起日子 YYYY-MM
    private String deadline;       // 時限要求 YYYY-MM
    private String progress;       // 當前進度: 未開展 / 推進中 / 已完成
    private String department;     // 責任部門
    private boolean overdue;       // 是否超期未反饋
    private String feedbackDeadline; // 反饋時限 YYYY-MM
}

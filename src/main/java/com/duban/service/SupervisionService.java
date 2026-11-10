package com.duban.service;

import com.duban.model.SupervisionItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupervisionService {

    private final List<SupervisionItem> items;

    public SupervisionService() {
        items = generateMockData();
    }

    private List<SupervisionItem> generateMockData() {
        List<SupervisionItem> list = new ArrayList<>();
        String[] departments = {"財務部", "人事部", "技術部", "市場部", "運營部", "法務部", "行政部"};
        String[] progresses = {"未開展", "推進中", "已完成"};
        String[] names = {
            "年度預算編制", "員工培訓計劃", "系統升級項目", "市場推廣活動",
            "客戶滿意度調查", "合規審查", "辦公設備採購", "數據安全加固",
            "品牌形象提升", "供應鏈優化", "人才招聘計劃", "產品迭代開發",
            "風險評估報告", "內部審計", "流程優化專項", "合作夥伴對接",
            "數字化轉型", "成本控制方案", "質量管理體系", "創新項目立項"
        };

        for (int i = 0; i < 25; i++) {
            String dept = departments[i % departments.length];
            String progress = progresses[i % 3];
            String start = String.format("2026-%02d", (i % 12) + 1);
            String deadline = String.format("2026-%02d", ((i % 12) + 4) % 12 + 1);
            String feedback = String.format("2026-%02d", ((i % 12) + 2) % 12 + 1);

            boolean overdue = progress.equals("未開展") || 
                (progress.equals("推進中") && YearMonth.parse(feedback).isBefore(YearMonth.now()));

            list.add(new SupervisionItem(
                (long) (i + 1),
                names[i % names.length] + " (" + (i + 1) + ")",
                start,
                deadline,
                progress,
                dept,
                overdue,
                feedback
            ));
        }
        return list;
    }

    public List<SupervisionItem> getAllItems() {
        return items;
    }

    public List<SupervisionItem> filterItems(String startDate, String endDate, 
                                             List<String> progresses, 
                                             boolean onlyUnreported) {
        return items.stream()
            .filter(item -> {
                if (startDate != null && !startDate.isEmpty()) {
                    if (item.getStartDate().compareTo(startDate) < 0) return false;
                }
                if (endDate != null && !endDate.isEmpty()) {
                    if (item.getStartDate().compareTo(endDate) > 0) return false;
                }
                if (progresses != null && !progresses.isEmpty()) {
                    if (!progresses.contains(item.getProgress())) return false;
                }
                if (onlyUnreported && !item.isOverdue()) return false;
                return true;
            })
            .collect(Collectors.toList());
    }
}

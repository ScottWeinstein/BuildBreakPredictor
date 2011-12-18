function [F1,p,r] = evalResults(predicted, actual, threshold)

    function prec = precision(tp, fp, ~)
        prec = tp / (tp + fp);
    end

    function rec = recall(tp, ~, fn)
        rec = tp / (tp + fn);
    end    
    function f = FOne(tp, fp, fn)
        prec = precision(tp, fp, fn);
        rec = recall(tp, fp, fn);
        f = (2 * prec * rec) / (prec + rec);
    end

    tp = sum((predicted >= threshold) & (actual == 1));
    fp = sum((predicted >= threshold) & (actual == 0));
    fn = sum((predicted < threshold) & (actual == 1));
    F1 = FOne(tp, fp, fn);
    p = precision(tp, fp, fn);
    r = recall(tp, fp, fn);
end
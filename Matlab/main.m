clear ; close all; clc;
[data,colheaders] = importfile('..\target\bt54.csv');
m = size(data,1);
X = data(:,2:end);
Y = data(:,1);

[Z,mu,sigma] = zscore(X(:,1:2));
X(:,1:2) = Z;

cvpart = cvpartition(Y,'holdout',0.3);
Xtrain = X(training(cvpart),:);
Ytrain = Y(training(cvpart),:);
Xtest = X(test(cvpart),:);
Ytest = Y(test(cvpart),:);
t = RegressionTree.template;
ens = fitensemble(Xtrain,Ytrain,'LogitBoost',180,t,'PredictorNames',colheaders(2:end));
plot(resubLoss(ens,'mode','cumulative'));
%[B,dev,stats] = glmfit(Xtrain,[ytrain ones(size(ytrain,1),1)], 'binomial', 'link', 'logit');
%yhat = glmval(B,X_valid, 'logit');
%yhat = yhat >= 0.8;
%validRes = sum((yhat - y_valid).^2)/size(yhat,1);
%plot(Xtrain, ytrain,'o',X_valid,yhat,'-','LineWidth',2);

predImp = predictorImportance(ens);

%out = cell(size(X,2),2);
%out(:,1) = colheaders(2:end)';
%out(:,2) = predImp';
out = strcat(colheaders', num2str(predRes'));
for ii = 1 : size(X,2)
    fprintf('%s\t\t%10.8f\n', colheaders{ii+1},predImp(ii));
%    out(ii,2) = mat2str(predImp(ii),5);
end
